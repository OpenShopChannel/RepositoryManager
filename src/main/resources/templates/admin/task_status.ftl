<#import "base.ftl" as base>

<@base.content>
    <div class="content">
        <#list messages as msg, class>
            <div class="alert alert-${class}">
                ${msg}
            </div>
            <br>
        </#list>

        <h1 class="content-title font-size-22"> <!-- font-size-22 = font-size: 2.2rem (22px) -->
            Task Status
        </h1>
        <div class="row">
            <div id="terminal" style="height: 600px; font-size: 16px;"></div>
        </div>
    </div>

    <link rel="stylesheet" href="/static/xterm/css/xterm.css" />
    <script src="/static/xterm/lib/xterm.js"></script>
    <script src="/static/stompjs/stomp.umd.min.js"></script>
    <script src="/static/stompjs/socket.js"></script>
    <script type="text/javascript" charset="utf-8">
        var term = new Terminal({
            rows: 40,
            cols: 160,
        });
        term.open(document.getElementById('terminal'));
        stompClient.activate();
    </script>
</@base.content>