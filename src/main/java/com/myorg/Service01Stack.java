package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.ILogGroup;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

public class Service01Stack extends Stack {

    public Service01Stack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        ApplicationLoadBalancedFargateService service = ApplicationLoadBalancedFargateService.Builder.create(this, "alb-01")
                .cpu(512)
                .desiredCount(2)
                .cluster(cluster)
                .listenerPort(8080)
                .memoryLimitMiB(1024)
                .assignPublicIp(true)
                .publicLoadBalancer(true)
                .serviceName("service-01")
                .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                        .image(ContainerImage.fromRegistry("gideonmarquesdasilva/curso-microservices-with-aws:1.0.0"))
                        .containerName("spring-with-aws")
                        .containerPort(8080)
                        .logDriver(
                                LogDriver.awsLogs(
                                        AwsLogDriverProps.builder()
                                                .logGroup(
                                                        LogGroup.Builder.create(this, "service-log-group")
                                                                .logGroupName("service")
                                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                                .build()
                                                )
                                                .streamPrefix("service")
                                                .build()
                                )
                        )
                        .build())
                .build();

        service.getTargetGroup().configureHealthCheck(HealthCheck.builder()
                .path("/actuator/health")
                .healthyHttpCodes("200")
                .port("8080")
                .build());

        ScalableTaskCount scalableTaskCount = service.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());

        scalableTaskCount.scaleOnCpuUtilization("service-auto-scaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());
    }
}
