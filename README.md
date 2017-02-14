# MQTT-with-Kaa-server

Porting MQTT for Kaa Open Source middleware IOT platform

## What is MQTT?

MQTT is a publish/subscribe, extremely simple and lightweight messaging protocol, designed for constrained devices and low-bandwidth, high-latency or unreliable networks. Since it is perfectly suitable for IOT/M2M communication, we opted to use MQTT. Moreover, we are also using it as communication channel between Kaa server and Dashboard.

For more details about MQTT, Kindly refer http://mqtt.org/

## How the MQTT communicate with Kaa server and external application?

![mqtt-kaa](https://raw.githubusercontent.com/ethicstechOSS/MQTT-with-Kaa-server/ethicstechOSS-images/mqtt_kaa.jpg)

We are here using MQTT at our dashboard side to exchange the datas between the dashboard application and Kaa server. Kaa server has log appenders such as MongoDB, Cassandra and other some databases. If we are using a dashboard, we need to stream the data directly to the dashboard which will cause more performance overhead in the databases. So we are using this MQTT protocol to append our logs to the MQTT server which can be subscribed and streaming data can be displayed. Since MQTT is open source, we opted to use it.

##  How to compile the source

1. Install dependencies

    NOTE : We tried the below steps on Ubuntu 16.04, 64bit version. Kaa server: 0.9.0

    Open terminal, do an update and then install mosquitto dependencies. To install mosquitto you must create a mosquitto user.

    ```bash
    sudo adduser mosquitto
    sudo apt-get update
    sudo apt-get install build-essential libwrap0-dev libssl-dev libc-ares-dev uuid-dev xsltproc
    sudo cd /home/mosquitto
    sudo wget http://mosquitto.org/files/source/mosquitto-1.4.8.tar.gz
    sudo tar xvzf mosquitto-1.4.8.tar.gz
    sudo cd mosquitto-1.4.8
    ```

    Run make to compile and make install:

    ```bash
    sudo make
    sudo make install
    ```

1.  Setup Mosquitto

    Create a mosquitto user/password: the command below will create a user owntracks, you can change

    ```bash
    sudo mkdir /var/lib/mosquitto/
    sudo chown mosquitto:mosquitto /var/lib/mosquitto/ -R
    sudo mosquitto_passwd -c /etc/mosquitto/pwfile YOUR_MOSQUITTO_USER_NAME
    ```

    Here `YOUR_MOSQUITTO_USER_NAME` - your mosquitto username

    you will be prompted to enter a password.
    Create the directory where persistence db files will be stored, change owner to mosquitto:

    ```bash
    sudo mkdir /var/lib/mosquitto/
    ```

    Create a config file by copying the example configuration:

    ```bash
    sudo cp /etc/mosquitto/mosquitto.conf.example /etc/mosquitto/mosquitto.conf
    ```

    Open the file by using any one of the editor like "gedit" or "vim" and at the end of the config file, add a block of all suggested configuation changes like the below.

    ```bash
    sudo gedit /etc/mosquitto/mosquitto.conf
    ```

    Replace `<yourIP>` with the IP address & port number.

    ```
    listener 8883 <yourIP>
    persistence true
    persistence_location /var/lib/mosquitto/
    persistence_file mosquitto.db
    log_dest syslog
    log_dest stdout
    log_dest topic
    log_type error
    log_type warning
    log_type notice
    log_type information
    connection_messages true
    log_timestamp true
    allow_anonymous false
    password_file /etc/mosquitto/pwfile
    ```

    Finally be sure to run:

    ```
    sudo /sbin/ldconfig
    ```


1.  Run/Test Mosquitto:

    ```bash
    sudo mosquitto -c /etc/mosquitto/mosquitto.conf
    ```

    It should start running without error.

    Then open another terminal/tab try the following command.

    ```bash
    sudo mosquitto_sub -h <YourIP> -p 8883 -v -t 'YOUR_TOPIC/#' -u YOUR_MOSQUITTO_USER_NAME -P YourPassword
    ```

    Example:
    ```bash
    sudo mosquitto_sub -h 192.168.0.105 -p 8883 -v -t 'ethicstech/#' -u ethicstech -P 1234
    ```

    If everything went correctly you should see no errors when executing this command and in the window where mosquitto is running should acknowledge the connection. if so, create an upstart file to autorun mosquitto:

    ```bash
    sudo gedit /etc/init/mosquitto.conf
    ```

    Then paste the below.

    ```bash
    description "Mosquitto MQTT broker"
    start on net-device-up
    respawn
    exec /usr/sbin/mosquitto -c /etc/mosquitto/mosquitto.conf
    ```

    Start Mosquitto server by using the following command:

    ```bash
    sudo service mosquitto srart
    ```

    Now you have a Mosquitto broker working on your server. For any further queries in the setup processof mosquitto refer the link: https://www.digitalocean.com/community/questions/how-to-setup-a-mosquitto-mqtt-server-and-receive-data-from-owntracks


1.  Paho MQTT Broker appender configuration on kaa server version 0.9.0.

    Compile the source code of mosquitto log appender for kaa server.

    Install git, maven and clone the source code from the repository.

    ```bash
    sudo apt-get install git maven
    git clone https://github.com/ethicstechOSS/MQTT-with-Kaa-server.git
    cd
    mvn clean install
    ```

    After the successful build, you will get the JAR file at /target/pahomqtt-appender-0.9.0.jar. The compiled JAR file also availale on our repository.

1.  Install log appender in kaa server

    - Copy the generated JAR file in the location at kaa node: /usr/lib/kaa-node/lib/
    - Restart the kaa server node services.

      ```
      sudo service kaa-node restart
      ```

1.  Add Log Appender to your application in Kaa

    - Login to your kaa server admin page using a developer account.
    - Under applications select-> YOUR_APPLICATION -> Log Appenders
    - Select add Log appender
    - Select the type as PahoMqtt and use the configruations as per your mosquitto server.
    - See the bellow screen shot.

![paho-kaa](https://raw.githubusercontent.com/ethicstechOSS/MQTT-with-Kaa-server/ethicstechOSS-images/paho_kaa.png)

