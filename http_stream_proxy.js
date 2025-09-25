#!/usr/bin/env node

/**
 * MCP HTTP Stream Proxy
 * 用于连接远程MCP HTTP Stream服务器并提供本地代理服务
 */

const http = require('http');
const https = require('https');
const url = require('url');

class MCPHttpStreamProxy {
    constructor(remoteUrl, localPort = 8082) {
        this.remoteUrl = remoteUrl;
        this.localPort = localPort;
        this.server = null;
        this.connections = new Map();
    }

    start() {
        this.server = http.createServer((req, res) => {
            this.handleRequest(req, res);
        });

        this.server.listen(this.localPort, () => {
            console.log(`🚀 MCP HTTP Stream Proxy listening on port ${this.localPort}`);
            console.log(`🔗 Proxying to: ${this.remoteUrl}`);
            console.log(`📡 SSE endpoint: http://localhost:${this.localPort}/connect`);
            console.log(`📤 Request endpoint: http://localhost:${this.localPort}/request`);
        });

        this.server.on('error', (err) => {
            if (err.code === 'EADDRINUSE') {
                console.error(`❌ Port ${this.localPort} is already in use`);
                console.log(`Try using a different port: node http_stream_proxy.js ${this.remoteUrl} ${this.localPort + 1}`);
            } else {
                console.error('❌ Server error:', err);
            }
        });
    }

    handleRequest(req, res) {
        // 设置CORS头
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Client-ID');
        res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');

        if (req.method === 'OPTIONS') {
            res.writeHead(200);
            res.end();
            return;
        }

        console.log(`📥 ${req.method} ${req.url}`);

        if (req.url === '/connect' || req.url === '/connect/') {
            this.handleSSEConnection(req, res);
        } else if (req.url === '/request' || req.url === '/request/') {
            this.handleMCPRequest(req, res);
        } else if (req.url === '/health' || req.url === '/health/') {
            this.handleHealthCheck(req, res);
        } else {
            res.writeHead(404, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ error: 'Not Found' }));
        }
    }

    handleSSEConnection(req, res) {
        const clientId = this.generateClientId();
        console.log(`🔌 New SSE connection: ${clientId}`);

        res.writeHead(200, {
            'Content-Type': 'text/event-stream',
            'Cache-Control': 'no-cache',
            'Connection': 'keep-alive',
            'Access-Control-Allow-Origin': '*'
        });

        // 发送连接确认
        this.sendSSEMessage(res, 'connection', {
            status: 'connected',
            clientId: clientId,
            timestamp: new Date().toISOString()
        });

        // 连接到远程服务器
        const remoteUrl = new URL(`${this.remoteUrl}/connect`);
        const protocol = remoteUrl.protocol === 'https:' ? https : http;
        
        const remoteReq = protocol.get(remoteUrl, (remoteRes) => {
            console.log(`🌐 Connected to remote server: ${remoteUrl.href}`);

            remoteRes.on('data', (chunk) => {
                res.write(chunk);
            });

            remoteRes.on('end', () => {
                console.log(`🔌 Remote connection ended for client: ${clientId}`);
                this.sendSSEMessage(res, 'disconnect', {
                    clientId: clientId,
                    timestamp: new Date().toISOString()
                });
                res.end();
            });

            remoteRes.on('error', (err) => {
                console.error(`❌ Remote connection error for client ${clientId}:`, err);
                this.sendSSEMessage(res, 'error', {
                    clientId: clientId,
                    error: err.message,
                    timestamp: new Date().toISOString()
                });
                res.end();
            });
        });

        remoteReq.on('error', (err) => {
            console.error(`❌ Failed to connect to remote server:`, err);
            this.sendSSEMessage(res, 'error', {
                clientId: clientId,
                error: `Failed to connect to remote server: ${err.message}`,
                timestamp: new Date().toISOString()
            });
            res.end();
        });

        req.on('close', () => {
            console.log(`🔌 Client disconnected: ${clientId}`);
            remoteReq.destroy();
        });

        this.connections.set(clientId, { req, res, remoteReq });
    }

    handleMCPRequest(req, res) {
        let body = '';
        req.on('data', chunk => {
            body += chunk.toString();
        });

        req.on('end', () => {
            console.log(`📤 Forwarding MCP request: ${body.substring(0, 100)}...`);

            // 转发请求到远程服务器
            const remoteUrl = new URL(`${this.remoteUrl}/request`);
            const protocol = remoteUrl.protocol === 'https:' ? https : http;
            
            const options = {
                hostname: remoteUrl.hostname,
                port: remoteUrl.port,
                path: remoteUrl.pathname,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Content-Length': Buffer.byteLength(body)
                }
            };

            const proxyReq = protocol.request(options, (proxyRes) => {
                console.log(`📥 Remote response status: ${proxyRes.statusCode}`);
                res.writeHead(proxyRes.statusCode, proxyRes.headers);
                proxyRes.pipe(res);
            });

            proxyReq.on('error', (err) => {
                console.error('❌ Proxy request error:', err);
                res.writeHead(500, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ 
                    error: 'Proxy Error', 
                    message: err.message,
                    timestamp: new Date().toISOString()
                }));
            });

            proxyReq.write(body);
            proxyReq.end();
        });
    }

    handleHealthCheck(req, res) {
        const healthInfo = {
            status: 'UP',
            service: 'MCP HTTP Stream Proxy',
            version: '1.0.0',
            remoteUrl: this.remoteUrl,
            activeConnections: this.connections.size,
            timestamp: new Date().toISOString()
        };

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(healthInfo));
    }

    sendSSEMessage(res, eventType, data) {
        const message = `event: ${eventType}\ndata: ${JSON.stringify(data)}\n\n`;
        res.write(message);
    }

    generateClientId() {
        return 'client_' + Math.random().toString(36).substr(2, 9);
    }

    stop() {
        if (this.server) {
            this.server.close();
            console.log('🛑 Proxy server stopped');
        }
    }
}

// 主程序
function main() {
    const args = process.argv.slice(2);
    
    if (args.length === 0) {
        console.log('Usage: node http_stream_proxy.js <remote-url> [local-port]');
        console.log('Example: node http_stream_proxy.js http://your-server.com:8081 8082');
        process.exit(1);
    }

    const remoteUrl = args[0];
    const localPort = args[1] ? parseInt(args[1]) : 8082;

    // 验证远程URL
    try {
        new URL(remoteUrl);
    } catch (err) {
        console.error('❌ Invalid remote URL:', remoteUrl);
        process.exit(1);
    }

    const proxy = new MCPHttpStreamProxy(remoteUrl, localPort);
    
    // 优雅关闭
    process.on('SIGINT', () => {
        console.log('\n🛑 Shutting down proxy server...');
        proxy.stop();
        process.exit(0);
    });

    process.on('SIGTERM', () => {
        console.log('\n🛑 Shutting down proxy server...');
        proxy.stop();
        process.exit(0);
    });

    proxy.start();
}

if (require.main === module) {
    main();
}

module.exports = MCPHttpStreamProxy;
