const http = require('http');

const BASE_URL = 'http://localhost:5178';
let sessionId = null;

function sseConnect() {
    return new Promise((resolve, reject) => {
        console.log('=== Connecting to SSE ===');
        const req = http.get(`${BASE_URL}/sse`, {
            headers: { 'Accept': 'text/event-stream' }
        }, (res) => {
            res.on('data', (chunk) => {
                const lines = chunk.toString().split('\n');
                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        const data = line.substring(5).trim();
                        if (data.includes('sessionId=')) {
                            sessionId = data.split('sessionId=')[1];
                            console.log(`Got session ID: ${sessionId}`);
                            resolve();
                        }
                    }
                }
            });
        });
        req.on('error', reject);
        req.setTimeout(5000, () => {
            req.destroy();
            reject(new Error('SSE timeout'));
        });
    });
}

function postJson(method, params = {}) {
    return new Promise((resolve, reject) => {
        const data = JSON.stringify({
            jsonrpc: '2.0',
            id: Date.now(),
            method,
            params
        });
        
        const url = `${BASE_URL}/mcp/message?sessionId=${sessionId}`;
        const req = http.request(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(data)
            }
        }, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                try {
                    resolve(JSON.parse(body));
                } catch (e) {
                    reject(e);
                }
            });
        });
        req.on('error', reject);
        req.write(data);
        req.end();
    });
}

async function main() {
    try {
        await sseConnect();
        
        console.log('\n=== Initialize ===');
        const init = await postJson('initialize', {
            protocolVersion: '2024-11-05',
            capabilities: {},
            clientInfo: { name: 'test-client', version: '1.0' }
        });
        console.log(JSON.stringify(init, null, 2));
        
        console.log('\n=== List Tools ===');
        const tools = await postJson('tools/list');
        console.log(JSON.stringify(tools, null, 2));
        
        console.log('\n=== Call task_list ===');
        const result = await postJson('tools/call', {
            name: 'task_list',
            arguments: {}
        });
        console.log(JSON.stringify(result, null, 2));
        
    } catch (e) {
        console.error('Error:', e.message);
    }
}

main();
