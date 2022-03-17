## dashboard

#### For running this project you must install dependencies with npm
```bash
 npm install
```

#### For running server you need to start the serve with
```bash
 gulp serve
```

#### For build the application, you must copy dist folder to /control-panel/src/main/resources/static/dashboards in order to integrate this app
```bash
 gulp build
```

#### For build the application for dev tools and then paste it into /dev/local-libs in dev-tools enviroment
```
 gulp build-dev
```

#### Temporary solution. If you want to develop this framework with osp...
You need to download nginx https://nginx.org/en/download.html and put in nginx.conf the following config:

```
#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;
	client_max_body_size 100M;

    #gzip  on;

    server {
        listen       8087;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;
		
		location /controlpanel {
			proxy_pass http://localhost:18000;
		}
		
		#location /dashboardengine {
		#	proxy_pass http://localhost:8089;
		#}
		
		location /dashboardengine/loginRest {
			proxy_pass http://localhost:18300;
		}
		
		location /dashboardengine/dsengine/solver { # For websocket support
			proxy_set_header X-Real-IP $remote_addr;
			proxy_set_header Host $host;
			proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
			proxy_pass http://localhost:18300;
			proxy_http_version 1.1;
			proxy_set_header Upgrade websocket;
			proxy_set_header Connection upgrade;
			proxy_read_timeout 86400;
		}
		
		location /exfront {
			proxy_pass http://localhost:88;
		}
		
		location / {
			proxy_pass http://localhost:3000;
		}

        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}

        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }


    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}


    # HTTPS server
    #
    #server {
    #    listen       443 ssl;
    #    server_name  localhost;

    #    ssl_certificate      cert.pem;
    #    ssl_certificate_key  cert.key;

    #    ssl_session_cache    shared:SSL:1m;
    #    ssl_session_timeout  5m;

    #    ssl_ciphers  HIGH:!aNULL:!MD5;
    #    ssl_prefer_server_ciphers  on;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}

}
```

Then you need to start control panel, dashboardengine, rtdb, bdc y quasar-engine

To enter the console use this access url http://localhost:8087/controlpanel
