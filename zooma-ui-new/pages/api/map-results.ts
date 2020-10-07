

import apiProxy from './helpers/api-proxy'

export default apiProxy('/services/map?json')

export const config = {
  api: {
    externalResolver: true,
    bodyParser: false
  },
}
