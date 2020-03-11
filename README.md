# PorteroSmart
Proyecto Final carreara de Ingeniería en Electrónica de la Universidad Tecnológica Nacional de Argentina.

Sede: Regional Buenos Aires.

Año: 2019

# Descripción del Proyecto
Con un módulo ESP8266 y una cámara RTSP se arma un sistema para reemplazar porteros de edificios.

El módulo ESP se conecta a una red WiFi local y se conecta los distintos timbres del portero existente. El módulo publica mensajes en un tópico de AWS MQTT.
La cámara se conecta al mismo Wifi y se configura el router para fordwardear el puerto 554, además se le configura una DDNS para mantener la dirección fija.

# Aplicación para el Poryecto Portero Smart.

La aplicación sirve como Display para una cámara con protocolo RTSP instalada en un edificio.

Utiliza la libreria VLC para capturar audio y video de la cámara.

A su vez lanza un servicio con la libreria JobIntentService que se conecta a AWS MQTT y se subscribe al tópico MQTT de Amazon al cual el ESP publica.

Al recibir un mensaje de ese tópico se lanaza una push notification para 
