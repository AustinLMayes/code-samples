## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

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
