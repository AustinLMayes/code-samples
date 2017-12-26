module LayoutsHelper
  # Render a view inside of the specified layout file.
  def inside_layout(parent_layout)
    view_flow.set :layout, capture { yield }
    render template: "layouts/#{parent_layout}"
  end
end