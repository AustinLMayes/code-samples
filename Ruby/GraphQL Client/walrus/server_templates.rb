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
  module ServerTemplates
    include ::Walrus

    TEMPLATES_QUERY = GraphQuery.new("templates/all").create({})
    TEMPLATE_ID_QUERY = GraphQuery.new("templates/by_id")

    def template_idents
      request(TEMPLATES_QUERY)[:serverTemplates].map { |t| t[:identifier] }
    end

    def all_templates
      request(TEMPLATES_QUERY)[:serverTemplates]
    end

    def template_by_id(id)
      request(TEMPLATE_ID_QUERY.create({id: id}))[:serverTemplates]
    end

    def get_template(identifier)
      all_templates.each do |temp|
        if temp[:identifier].downcase == identifier.downcase
          return fix_template(temp)
        end
      end
      nil
    end

    private

    def fix_template(temp)
      temp[:plugins].map! { |pl| JSON.parse(pl.gsub('=>', ':'), symbolize_names: true) } if temp[:plugins]
      temp[:components].map! { |comp| JSON.parse(comp.gsub('=>', ':'), symbolize_names: true) } if temp[:components]
      temp[:remote_files].map! { |file| JSON.parse(file.gsub('=>', ':'), symbolize_names: true) } if temp[:remote_files]
      return temp
    end
  end
end
