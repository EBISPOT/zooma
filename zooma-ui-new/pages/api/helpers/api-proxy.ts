
import { createProxyMiddleware } from "http-proxy-middleware";

let apiURL = process.env.ZOOMA_API_URL

export default function apiProxy(path) {
    return createProxyMiddleware({
        target: apiURL + path,
        ignorePath: true,
        secure: false,
        onProxyRes: (proxyRes, req, res) => {
          if(proxyRes.headers['set-cookie'] !== undefined)
            proxyRes.headers['set-cookie'] = [
               proxyRes.headers['set-cookie'][0]
                .replace(/Path=.*;/g, '')
            ]
        },
        onProxyReq: (proxyReq, req:Request, res:Response) => {
          
        }
    });
}

export const config = {
  api: {
    externalResolver: true,
    bodyParser: false
  },
}
