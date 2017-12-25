yaml = YAML.load(File.open(Rails.root.join('config/avicus.yml')))

unless yaml['default']
  raise('No default path in avicus.yml')
end

$avicus = yaml['default']
$avicus = $avicus.deep_merge(yaml[Rails.env]) if yaml[Rails.env]

# Action Mailer
smtp = $avicus['smtp']
unless smtp.nil?
  ActionMailer::Base.delivery_method = :smtp
  ActionMailer::Base.smtp_settings = {
      address: smtp['host'],
      port: smtp['port'],
      authentication: 'plain',
      domain: smtp['domain'],
      user_name: smtp['username'],
      password: smtp['password'],
      enable_starttls_auto: true
  }
end
