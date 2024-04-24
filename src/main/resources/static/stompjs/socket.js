const stompClient = new StompJs.Client({
    brokerURL: (location.protocol === 'https:' ? 'wss' : 'ws') + '://' + location.host + '/socket'
});

stompClient.onConnect = (frame) => {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/logUpdate', (payload) => {
        let line = JSON.parse(payload.body);
        let now = new Date();
        let timestamp = '[' + now.toUTCString() + '] ';

        let prefix;
        switch (line.status) {
            case "info":
                // reset
                prefix = '\x1b[0m'
                break;
            case "error":
                // red
                prefix = '\x1b[1;31m'
                break;
            case "success":
                // green
                prefix = '\x1b[1;32m'
                break;
            case "warn":
                // yellow
                prefix = '\x1b[1;33m'
                break;
            case "title":
                // cyan
                prefix = '\x1b[1;36m'
                break;
            default:
                prefix = '\x1b[0m'
        }

        term.writeln(prefix + timestamp + line.message);
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};