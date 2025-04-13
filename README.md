```bash
mvn dependency:list         # List all dependencies
mvn dependency:tree        # Show dependency hierarchy
mvn dependency:analyze     # Find unused/undeclared dependencies
mvn dependency:copy-dependencies  # Copy dependencies to target/libs
mvn dependency:purge-local-repository  # Clean local repository
mvn dependency:resolve     #
```

```
└── 📁src
    └── 📁main
        └── 📁java
            └── 📁com
                └── 📁example
                    └── 📁shelldemo
                        └── 📁agent
                            └── AgentAttacher.java
                            └── RuntimeAgent.java
                        └── 📁analysis
                            └── AlertManager.java
                            └── RuntimeAnalysisDocumentation.java
                            └── RuntimeAnalysisRunner.java
                            └── RuntimeAnalyzer.java
                        └── 📁annotation
                            └── Command.java
                        └── App.java
                        └── 📁cli
                            └── CommandGenerator.java
                            └── DocumentationGenerator.java
                            └── UnifiedDatabaseRunner.java
                        └── 📁config
                        └── 📁datasource
                            └── AbstractDatabaseConnection.java
                            └── AbstractDatabaseOperation.java
                            └── UnifiedDatabaseOperation.java
                        └── 📁exception
                            └── AgentException.java
                            └── BaseException.java
                            └── GlobalExceptionHandler.java
                            └── MonitoringException.java
                            └── RuntimeAnalysisException.java
                        └── 📁model
                            └── ConnectionConfig.java
                        └── 📁monitoring
                            └── AlertManager.java
                            └── 📁collector
                                └── CommandMetricsCollector.java
                                └── CustomCollectorLoader.java
                                └── JvmMetricsCollector.java
                                └── SystemMetricsCollector.java
                            └── MetricsManager.java
                            └── MetricsPersistence.java
                            └── 📁model
                                └── Alert.java
                                └── AlertConfig.java
                                └── AlertRule.java
                                └── MetricBuffer.java
                                └── MetricCollector.java
                                └── MetricEvent.java
                            └── NotificationService.java
                        └── 📁service
                            └── CommandService.java
                        └── 📁util
                            └── CommandClassDiscoverer.java
                            └── CommandData.java
                            └── CommandRegistry.java
                            └── ScriptGenerator.java
                            └── ScriptManager.java
        └── 📁resources
            └── alert-config-schema.yaml
            └── logback.xml
            └── 📁META-INF
                └── MANIFEST.MF
            └── 📁oracle_init_scripts
                └── create_oracle_image.sh
                └── Dockerfile
                └── 📁sql
                    └── 1_create_hr_userl.sql
                    └── 2_create_hr_tables.sql
                    └── 3_populate.sql
                    └── 4_others.sql
                └── test.sql
            └── 📁static
                └── search.js
                └── styles.css
            └── 📁templates
                └── application.properties.ftl
                └── application.yaml.ftl
                └── interactive-docs.ftl
                └── junit-tests.java.ftl
                └── metrics-dashboard.ftl
                └── openapi.yaml.ftl
    └── 📁test
        └── 📁java
            └── 📁com
                └── 📁example
                    ├── shelldemo
```

## Enable SSH 

jk
```bash
PubkeyAuthentication yes                                                                                        
                                                                                                                
# Expect .ssh/authorized_keys2 to be disregarded by default in future.                                          
AuthorizedKeysFile .ssh/authorized_keys .ssh/authorized_keys2
```


To ensure your Ubuntu WSL2 instance stays updated automatically, you can utilize the unattended-upgrades tool. This tool automatically downloads and installs security updates and other essential upgrades for your server. [1, 2]  

Here's how to enable and configure it: 
1. Install the unattended-upgrades package: 

```
sudo apt update
sudo apt install unattended-upgrades
```

 [1, 3]  
1. Configure the unattended-upgrades file: [2, 2]  
Edit the file /etc/apt/apt.conf.d/50unattended-upgrades to control the behavior of the tool: [2, 2, 4, 5]  
sudo nano /etc/apt/apt.conf.d/50unattended-upgrades

 [2]  
3. Enable automatic upgrades: 
In the configuration file, ensure the following lines are present and set to "1": [4, 6, 7]  
Acquire::http::No-Proxy "http://";
Acquire::https::No-Proxy "https://";
Unattended-Upgrade::Allowed-Origin "http://";
Unattended-Upgrade::Allowed-Origin "https://";
Unattended-Upgrade::Automatic-Downgrade "false";
Unattended-Upgrade::Package-Policy "install-only";
Unattended-Upgrade::Policy-Update-New-Debian "1";
Unattended-Upgrade::Policy-Update-Installed "1";

 [2, 8]  
4. Enable the apt-daily-upgrade timer: [2, 9, 10, 11]  
This timer schedules the unattended-upgrades script to run daily: [2, 9, 12, 13]  
sudo systemctl enable apt-daily-upgrade.timer
sudo systemctl start apt-daily-upgrade.timer

 [2, 9]  
5. Test the configuration: [2, 2]  
You can manually trigger the update process to verify that everything is working correctly: [2, 14]  
sudo unattended-upgrades

 [2]  

Generative AI is experimental.

[1] https://www.digitalocean.com/community/tutorials/how-to-keep-ubuntu-22-04-servers-updated[2] https://ubuntu.com/blog/ubuntu-updates-best-practices-for-updating-your-instance[3] https://phoenixnap.com/kb/automatic-security-updates-ubuntu[4] https://askubuntu.com/questions/9/how-do-i-enable-automatic-updates[5] https://askubuntu.com/questions/656041/which-unattended-upgrades-system-takes-precedence-and-how[6] https://github.com/mvo5/unattended-upgrades/blob/master/README.md[7] https://medium.com/@sonoratek/effortless-ubuntu-server-maintenance-enable-automatic-updates-in-just-5-minutes-e53c0eadd9a6[8] https://www.youtube.com/watch?v=y5_wPaHlcHQ[9] https://askubuntu.com/questions/172524/how-can-i-check-if-automatic-updates-are-enabled-and-disable-them-if-theyre-ena[10] https://github.com/QubesOS/qubes-issues/issues/2621[11] https://help.ubuntu.com/community/AutomaticSecurityUpdates[12] https://askubuntu.com/questions/989742/unattended-upgrade-update-everything-out-of-everytihng-silently-without-any[13] https://www.jwillikers.com/unattended-upgrades[14] https://www.reddit.com/r/Ubuntu/comments/124tf5u/anybody_know_whats_happening_here/
