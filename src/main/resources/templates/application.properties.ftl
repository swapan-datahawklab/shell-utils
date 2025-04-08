# Application Properties
# Generated on ${.now?string("yyyy-MM-dd HH:mm:ss")}

<#list commands as command>
# ${command.name} Configuration
<#list command.options as option>
<#if option.names?size gt 0>
# ${option.description}
${option.names[0]}=${option.defaultValue!""}
</#if>
</#list>

</#list>

# Database Configuration
database.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB
database.username=system
database.password=oracle

# Logging Configuration
logging.level.root=INFO
logging.level.com.example=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n