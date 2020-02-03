<p align="center">
  <a src='https://www.onesaitplatform.com/'>
    <img src='../resources/images/onesait-platform-logo.png'/>
  </a>
</p>

# Sources of onesait Platform (Cloud version)
![Build Status](https://sofia2-devops.westeurope.cloudapp.azure.com/jenkins/buildStatus/icon?job=onesait-platform/master)

## Sources Structure

The sources of the platform follows this skeleton:

*  [Libraries](libraries/) source code to different utilities of the platform used on the differente deployable modules (audit, config, themes,...)
   * [Client Libraries](libraries/client-libraries/) contains the source code of the SDKs for access the platform from different languages (Java, Javascript, Android, Python,...)
   * [Persistence](persistence/) source code for everything related to the persistence in the platform (Mongo as RI Persistence, abstraction services,...)
   * [Security](security/) source code for everything related to the security in the platform. We include reference implementations based on ConfigDB.
*  [Services](services/) source code to services of the platform used on the differente deployable modules (mail, twitter, persistence,...)
*  [Modules](modules/) source code of the different executables of the platform (API Manager, IoTBroker, Control Panel, ...)
*  [Examples](examples/) contains different examples that help us to develop with and to extend the platform.
