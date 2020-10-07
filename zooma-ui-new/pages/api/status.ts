

import apiProxy from './helpers/api-proxy'

export default apiProxy('/services/map/status')

export const config = {
  api: {
    externalResolver: true,
    bodyParser: false
  },
}
