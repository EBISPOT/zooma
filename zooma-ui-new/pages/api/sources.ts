

import apiProxy from './helpers/api-proxy'

export default apiProxy('/sources')

export const config = {
  api: {
    externalResolver: true,
    bodyParser: false
  },
}
