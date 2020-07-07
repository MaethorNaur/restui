import PropTypes from 'prop-types'
import React, { Component } from 'react'
import { withRouter } from 'react-router-dom'
import NoService from './noService'
import SwaggerUI from 'swagger-ui'

const RestUILayoutPlugin = () => {
  return {
    components: {
      InfoUrl: () => null
    }
  }
}

class Swagger extends Component {
  constructor (props) {
    super(props)
    this.SwaggerUIComponent = null
    this.system = null
  }

  componentDidMount () {
    const id = this.props.location.pathname.substring(1)
    let url = ''
    if (id) {
      url = `/services/${id}`
    }

    const ui = SwaggerUI({
      url,
      docExpansion: 'list',
      plugins: [RestUILayoutPlugin]
    })

    this.system = ui
    this.SwaggerUIComponent = ui.getComponent('App', 'root')

    this.forceUpdate()
  }

  componentDidUpdate (prevProps) {
    const id = this.props.location.pathname.substring(1)
    if (id !== prevProps.location.pathname.substring(1)) {
      this.system.specActions.updateSpec('')

      if (id) {
        const url = `/services/${id}`
        this.system.specActions.updateUrl(url)
        this.system.specActions.download(url)
      }
    }
  }

  render () {
    const id = this.props.location.pathname.substring(1)
    return (
      <div>
        {id ? (
          this.SwaggerUIComponent ? (
            <this.SwaggerUIComponent />
          ) : null
        ) : (
          <NoService />
        )}
      </div>
    )
  }
}

Swagger.propTypes = {
  match: PropTypes.object.isRequired,
  location: PropTypes.object.isRequired,
  history: PropTypes.object.isRequired
}

export default withRouter(Swagger)
