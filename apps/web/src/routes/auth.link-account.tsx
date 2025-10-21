import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/auth/link-account')({
  component: RouteComponent,
})

function RouteComponent() {
  return <div>Hello "/auth/link-account"!</div>
}
