Para que el SPI de Hazelcast para Kubernetes tenga acceso al API de Kubernetes, es necesario concederle permisos.
Sustituir en la siguiente instrucicón <kubernetes_namespace> por el valor correspondiente y Ejectuar en kubectl

	> kubectl create clusterrolebinding <kubernetes_namespace>:default --clusterrole=cluster-admin --serviceaccount=<kubernetes_namespace>:default