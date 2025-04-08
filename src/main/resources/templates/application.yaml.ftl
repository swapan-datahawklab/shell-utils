# Application Configuration
# Generated on ${.now?string("yyyy-MM-dd HH:mm:ss")}

<#list commands as command>
${command.name}:
  description: ${command.description}
  options:
<#list command.options as option>
<#if option.names?size gt 0>
    ${option.names[0]?replace("--", "")}:
      description: ${option.description}
      type: ${option.type}
      required: ${option.required?string("true", "false")}
      default: ${option.defaultValue!""}
</#if>
</#list>
</#list>

database:
  url: jdbc:oracle:thin:@localhost:1521:ORCLCDB
  username: system
  password: oracle

logging:
  level:
    root: INFO
    com.example: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"