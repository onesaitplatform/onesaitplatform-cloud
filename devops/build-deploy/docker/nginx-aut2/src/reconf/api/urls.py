from django.urls import path

from . import views

urlpatterns = [
    path('nginx', views.nginx_get, name='nginx_get'),
    path('nginx/version/<int:version>', views.nginx_get, name='nginx_get'), # GET
    path('nginx/versions', views.nginx_get_versions, name='nginx_get_versions'), # GET
    path('nginx/undo', views.nginx_undo, name='nginx_undo_last'), # PUT
    path('nginx/undo/<int:version>', views.nginx_undo, name='nginx_undo'), # PUT
    path('nginx/reset', views.nginx_reset, name='nginx_reset'), # PUT
    path('nginx/test', views.nginx_test_config, name='nginx_test'), # POST
    path('nginx/set', views.nginx_set_config, name='nginx_set'), # POST
]
