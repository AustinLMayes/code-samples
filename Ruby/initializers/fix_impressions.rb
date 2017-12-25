ImpressionistController::InstanceMethods.class_eval do
  def session_hash
    session[:init] = true
    request.session.id
  end
end