services {
  name = "controlpanel"
  port = 18000
  address= "controlpanelservice"
  connect {
    sidecar_service {
      address="onesaitplatform-controlpanelservice-controlpanel-sidecar-1"
      port=20001
      check {
        name = "controlpanel-sidecar"
        tcp = "onesaitplatform-controlpanelservice-controlpanel-sidecar-1:20001"
        interval ="10s"
      }
      proxy {
      local_service_address= "controlpanelservice"
      upstreams {
          destination_name = "quasar"
          local_bind_address = "onesaitplatform-controlpanelservice-controlpanel-sidecar-1"
          local_bind_port = 11800
        }
      }
    }
  }
}
services {
  name = "quasar"
  port = 10800
  address= "quasar"
  connect {
    sidecar_service {
      address="onesaitplatform-quasar-quasar-sidecar-1"
      port=20000
      check {
        name = "quasar-sidecar"
        tcp = "onesaitplatform-quasar-quasar-sidecar-1:20000"
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

services {
  name = "realtimedb"
  port = 27017
  address= "realtimedb"
  connect {
    sidecar_service {
      address="onesaitplatform-realtimedb-realtimedb-sidecar-1"
      port=20000
      check {
        name = "quasar-sidecar"
        tcp = "onesaitplatform-realtimedb-realtimedb-sidecar-1:20000"
        interval ="10s"
      }
      proxy{
        local_service_address= "quasar"
      }
    }
  }
}