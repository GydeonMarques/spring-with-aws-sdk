package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VpcStack extends Stack {

    private final Vpc vpc;

    public VpcStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public VpcStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        this.vpc = Vpc.Builder.create(this, "VPC-01")
                .natGateways(0)
                .maxAzs(2)
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
