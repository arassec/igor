# RabbitMQ Message Service
A service that uses RabbitMQ to send messages.

# Parameters
The RabbitMQ service can be configured with the following parameters:

Parameter | Description
---|:---|
Host | The host running RabbitMQ.
Port | The port, RabbitMQ is listening on.
Username | The RabbitMQ user's name.
Password | The password of the RabbitMQ user.
Exchange | The exchange that should be used for sending messages.
Routing key | An optional routing key that will be used in messages of this service.
Virtual host | The virtual host to use on the RabbitMQ server.
Content encoding | Sets the encoding for messages sent by this service.
Headers | Each line can contain a 'Header=Value'-pair which is used in messages sent by this service.
Heart beat | RabbitMQ heart beat in seconds to use to check the connection to the server.
Connection timeout | A timeout in milliseconds after which a connection to the server will be aborted.
