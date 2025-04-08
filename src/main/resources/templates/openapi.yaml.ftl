openapi: 3.0.0
info:
  title: Command Line Interface API
  description: API documentation for the command line interface
  version: 1.0.0
  contact:
    name: API Support
    email: support@example.com

servers:
  - url: http://localhost:8080
    description: Local development server

paths:
<#list commands as command>
  /${command.name}:
    post:
      summary: Execute ${command.name} command
      description: ${command.description}
      operationId: execute${command.name?cap_first}
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
<#list command.options as option>
<#if option.names?size gt 0>
                ${option.names[0]?replace("--", "")}:
                  type: ${option.type}
                  description: ${option.description}
                  required: ${option.required?string("true", "false")}
</#if>
</#list>
      responses:
        '200':
          description: Command executed successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  status:
                    type: string
                    example: "success"
                  message:
                    type: string
                    example: "Command executed successfully"
        '400':
          description: Invalid command parameters
          content:
            application/json:
              schema:
                type: object
                properties:
                  status:
                    type: string
                    example: "error"
                  message:
                    type: string
                    example: "Invalid parameters"
</#list>

components:
  schemas:
    Error:
      type: object
      properties:
        status:
          type: string
        message:
          type: string