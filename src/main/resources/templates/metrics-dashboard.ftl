<!DOCTYPE html>
<html>
<head>
    <title>Real-time Metrics Dashboard</title>
    <script src="/webjars/sockjs-client/sockjs.min.js"></script>
    <script src="/webjars/stomp-websocket/stomp.min.js"></script>
    <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
    <style>
        .metric-card {
            border: 1px solid #ddd;
            padding: 15px;
            margin: 10px;
            border-radius: 5px;
        }
        .alert {
            background-color: #ff4444;
            color: white;
            padding: 10px;
            margin: 10px;
            border-radius: 5px;
        }
    </style>
</head>
<body>
    <div id="alerts"></div>
    <div class="metrics-grid">
        <#list collectors as collector>
            <div class="metric-card">
                <h3>${collector.name}</h3>
                <div id="chart-${collector.name}" class="chart"></div>
            </div>
        </#list>
    </div>

    <script>
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);
        const charts = {};

        stompClient.connect({}, frame => {
            <#list collectors as collector>
                stompClient.subscribe('/topic/metrics/${collector.name}', message => {
                    updateChart('${collector.name}', JSON.parse(message.body));
                });
            </#list>

            stompClient.subscribe('/topic/alerts', message => {
                showAlert(JSON.parse(message.body));
            });
        });

        function updateChart(collectorName, data) {
            if (!charts[collectorName]) {
                charts[collectorName] = initializeChart(collectorName);
            }
            
            const chart = charts[collectorName];
            Plotly.extendTraces(chart, {
                x: [[data.timestamp]],
                y: [[data.value]]
            }, [0]);
        }

        function showAlert(alert) {
            const alertDiv = document.createElement('div');
            alertDiv.className = 'alert';
            alertDiv.textContent = alert.message;
            document.getElementById('alerts').appendChild(alertDiv);
            
            setTimeout(() => alertDiv.remove(), 5000);
        }
    </script>
</body>
</html> 