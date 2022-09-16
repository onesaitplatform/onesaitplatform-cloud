- Configuración interna de elasticsearch:

/usr/share/elasticsearch/config/elasticsearch.yml

- Usuarios internos de OpenDistro:

/usr/share/elasticsearch/plugins/opendistro_security/securityconfig/internal_users.yml

- Herramienta de generación de claves para los usuarios:

/usr/share/elasticsearch/plugins/opendistro_security/tools/hash.sh

- Configuración de seguridad:

/usr/share/elasticsearch/plugins/opendistro_security/securityconfig/config.yml

  - jwt_auth_domain
  - basic_internal_auth_domain

- Usuario por defecto:

admin / admin

- Comprobar login básico:

curl -XGET https://elasticdb:9200 -u admin:admin -k

- Obtener índices y datos (con ssl activo):

curl -XGET --insecure https://elasticdb:9200/_cat/indices -u admin:admin

curl -XGET --insecure "https://elasticdb:9200/<indice>/_search?pretty" -H 'Content-Type: application/json' -u admin:admin

curl -XGET --insecure "https://elasticdb:9200/<indice>/_count?pretty" -H 'Content-Type: application/json' -u admin:admin

- Obtener índices y datos (sin ssl activo):

curl -XGET http://elasticdb:9200/_cat/indices -u admin:admin

curl -XGET "http://elasticdb:9200/<indice>/_search?pretty" -H 'Content-Type: application/json' -u admin:admin

curl -XGET "http://elasticdb:9200/<indice>/_count?pretty" -H 'Content-Type: application/json' -u admin:admin
