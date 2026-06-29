import { Component, type ReactNode, type ErrorInfo } from 'react'
import { AlertTriangle, RefreshCw } from 'lucide-react'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
}

export default class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('ErrorBoundary caught:', error, info)
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null })
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-admin-bg flex items-center justify-center p-8">
          <div className="max-w-md w-full p-8 rounded-3xl bg-admin-surface border border-admin-border flex flex-col items-center text-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-danger-red/10 flex items-center justify-center">
              <AlertTriangle className="w-7 h-7 text-danger-red" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-white">Something went wrong</h2>
              <p className="text-xs text-text-gray mt-1">
                {this.state.error?.message || 'An unexpected error occurred'}
              </p>
            </div>
            <button
              onClick={this.handleRetry}
              className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-biro-blue text-xs font-bold text-white hover:scale-105 transition-all cursor-pointer"
            >
              <RefreshCw className="w-4 h-4" /> Try Again
            </button>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}
