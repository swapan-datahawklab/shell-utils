<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Command Line Interface Documentation</title>
    <link rel="stylesheet" href="static/styles.css">
    <script src="static/highlight.pack.js"></script>
    <script src="static/search.js"></script>
</head>
<body>
    <div class="container">
        <header>
            <h1>Command Line Interface Documentation</h1>
            <div class="search-box">
                <input type="text" id="search" placeholder="Search commands...">
            </div>
        </header>

        <main>
            <#list commands as command>
                <section class="command-section" id="command-${command.name}">
                    <h2>${command.name}</h2>
                    <p class="description">${command.description}</p>

                    <#if command.options?has_content>
                        <h3>Options</h3>
                        <table class="options-table">
                            <thead>
                                <tr>
                                    <th>Option</th>
                                    <th>Description</th>
                                    <th>Type</th>
                                    <th>Required</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#list command.options as option>
                                    <tr>
                                        <td><code>${option.names?join(", ")}</code></td>
                                        <td>${option.description}</td>
                                        <td>${option.type}</td>
                                        <td>${option.required?string("Yes", "No")}</td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
                    </#if>

                    <#if command.parameters?has_content>
                        <h3>Parameters</h3>
                        <table class="parameters-table">
                            <thead>
                                <tr>
                                    <th>Parameter</th>
                                    <th>Description</th>
                                    <th>Index</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#list command.parameters as param>
                                    <tr>
                                        <td>${param.name}</td>
                                        <td>${param.description}</td>
                                        <td>${param.index}</td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
                    </#if>

                    <#if command.groups?has_content>
                        <h3>Option Groups</h3>
                        <#list command.groups as group>
                            <div class="option-group">
                                <h4>${group.name}</h4>
                                <p>Required: ${group.required?string("Yes", "No")}</p>
                                <p>Exclusive: ${group.exclusive?string("Yes", "No")}</p>
                            </div>
                        </#list>
                    </#if>
                </section>
            </#list>
        </main>

        <footer>
            <p>Generated on ${.now?string("yyyy-MM-dd HH:mm:ss")}</p>
        </footer>
    </div>
    <script src="static/script.js"></script>
</body>
</html>


