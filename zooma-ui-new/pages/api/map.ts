

import apiProxy from './helpers/api-proxy'

export default apiProxy('/services/map')

export const config = {
  api: {
    externalResolver: true,
    bodyParser: false
  },
}
