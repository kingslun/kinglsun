package com.kings.framework.k8s;

import org.springframework.beans.factory.Aware;

interface NamingAware extends Aware {
    String name();
}
