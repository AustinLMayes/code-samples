require_relative "api"
require_relative "../graph_query"
require 'json'

module Walrus
  module Servers
    include ::Walrus
    include ServerTemplates

    TEMPLATE_QUERY = GraphQuery.new("servers/template")
    ADD_MUTATION = GraphQuery.new("servers/add")
    CREATE_MUTATION = GraphQuery.new("servers/create")

    def template(server_id)
      template_by_id(request(TEMPLATE_QUERY.create({id: server_id}))[:findServer][:template_id])
    end

    def add_server(server_id, pod_name)
      request(ADD_MUTATION.create({id: server_id, pod_name: pod_name}))
    end

    def create_server(template_id)
      request(CREATE_MUTATION.create({template_id: template_id}))[:serverCreate][:serverId]
    end
  end
end
