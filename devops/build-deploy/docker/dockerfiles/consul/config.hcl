services {
  name = "controlpanel"
  port = 18000
  address= "192.168.1.4"
  connect {
    sidecar_service {
      address="10.5.0.3"
      port=20001
      check {
        name = "Connect Envoy Sidecar"
        tcp = "10.5.0.3:20001"
        interval ="10s"
      }
      proxy {
        upstreams {
          destination_name = "quasar"
          local_bind_address = "10.5.0.3"
          local_bind_port = 11800 
        }
      }
    }
  }
}
services {
  name = "quasar"
  port = 10800
  address= "10.5.0.4"
  connect {
    sidecar_service {
      address="10.5.0.6"
      port=20000  
      check {
        name = "Connect Envoy Sidecar"
        tcp = "10.5.0.6:20000"
        interval ="10s"
      }
      proxy{
        local_service_address= "quasar"
      }
    }
  }
}

connect {
  enabled = true
}