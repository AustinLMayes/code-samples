# A single-threaded worker that handles private server requests

require "kubeclient"
require "celluloid/current"
require "celluloid/io"

class PrivateServerManager
  include QueueWorker

  queue :use_server
  consumer exclusive: true, manual_ack: false

  topic_binding UseServerRequest
  topic_binding ModelUpdate

  around_event :dequeue do |_, yielder|
    ApiSyncable.syncing(&yielder)
  end

  empty_times = {}

  poll delay: 1.minutes do
    Server.owned.each do |s|
      next if s.num_online > 0 || !s.online?

      if empty_times.include? safe_name(s.bungee_name)
        if Time.now - empty_times[safe_name(s.bungee_name)] > 10.minutes
          # Kill empty servers
          s.queue_restart(reason: "Automated server reset", priority: Server::Restart::Priority::HIGH)
          empty_times.delete safe_name(s.bungee_name)
        end
      else
        empty_times[safe_name(s.bungee_name)] = Time.now
      end
    end

    # Keep 1 avaliable server ready at all times
    if Server.free_for_requests.empty?
      create_server
    end
  end

  handle ModelUpdate do |msg|
      if msg.model <= Server
          server = msg.document
          if !server.nil? && server.user.present? && !server.online?
            begin
              cluster.delete_pod safe_name(server.bungee_name), 'default'
            rescue
              # Pod already gone
            end

            begin
              cluster.delete_service safe_name(s.bungee_name), 'default'
            rescue
              # Service already gone
            end
          end
      end
  end

  handle UseServerRequest do |request|
    ApiSyncable.syncing do
      user = request.user
      server = Server.find_by(name: request.name)
      if server.nil?
        server = Server.free_for_requests.first
        if server.nil?
          server = create_server
        end
        claim_server(server, user, request.name)
      end
      create_pod(server) unless server_online?(server)
      res = UseServerResponse.new(request: request)
      res.server_name = server.bungee_name
      res.now = server.online?
      res
    end
  end

  protected

  def server_online?(s)
    begin
      p = cluster.get_pod(safe_name(s.bungee_name), 'default').status.phase
      p == "Running" || p == "Pending"
    rescue
      false
    end
  end

  def create_server
    index = Server.owned.size + Server.free_for_requests.size + 1
    name = "Requestable-#{index}"
    Server.create(
      name: name,
      bungee_name: safe_name(name.downcase),
      ip: name.downcase,
      priority: 40 + index,
      online: false,
      whitelist_enabled: true,
      maps_branch: 'private-server',
      plugins_path: 'private',
      settings_profile: "private",
      datacenter: "US",
      box: "production",
      family: "private",
      role: "PGM",
      network: "PUBLIC",
      visibility: "UNLISTED",
      startup_visibility: "UNLISTED",
      realms: ["global", "normal", "private"],
      operator_ids: [],
      ownable: true,
      user_id: nil
    )
  end

  def claim_server(server, user, server_name)
    name = server_name
    bungee_name = safe_name(server_name.downcase)
    ip = safe_name(bungee_name)
    server.update(name: name,
      bungee_name: bungee_name,
      ip: ip,
      operator_ids: [user._id],
      user: user
    )
  end

  def safe_name(name)
    name.gsub("_", "u")
  end

  def create_pod(server)
    logger.info "Creating service for " + server.name
    name_safe = safe_name(server.bungee_name)
    service = Kubeclient::Resource.new
    service.metadata = {
      name: name_safe,
      labels: {
        role: 'private',
        type: 'minecraft',
        user: name_safe
      },
      namespace: 'default'
    }
    service.spec = {
      clusterIP: 'None',
      ports: [
        {
          port: 25565,
          name: 'minecraft'
        }
      ],
      selector: {
        user: name_safe
      }
    }
    begin
      cluster.create_service(service)
    rescue
      logger.info "Failed to create sevice for " + server.name + " (likely already exists)"
    end
    logger.info "Creating pod for " + server.name
    pod = Kubeclient::Resource.new
    pod.metadata = {
      name: name_safe,
      labels: {
        role: 'private',
        type: 'minecraft',
        user: name_safe
      },
      namespace: 'default'
    }
    pod.spec = {
      nodeSelector: {
        private: 'true'
      },
      containers: [
        {
          name: 'minecraft',
          image: 'gcr.io/stratus-197318/minecraft:bukkit-master',
          imagePullPolicy: 'Always',
          ports: [
            {containerPort: 25565, name: 'minecraft', protocol: 'TCP'}
          ],
          readinessProbe: {
            initialDelaySeconds: 15,
            periodSeconds: 15,
            timeoutSeconds: 5,
            exec: {
              command: [
                'ruby',
                'run.rb',
                'ready?'
              ]
            }
          },
          livenessProbe: {
            initialDelaySeconds: 60,
            periodSeconds: 30,
            timeoutSeconds: 5,
            exec: {
              command: [
                'ruby',
                'run.rb',
                'alive?'
              ]
            }
          },
          stdin: true,
          tty: true,
          resources: {
            requests: {
              cpu: '100m',
              memory: '500Mi'
            }
          },
          envFrom: [
            {
              secretRef: {
                name: 'minecraft-secret'
              }
            }
          ],
          volumeMounts: [
            {
              name: 'maps',
              mountPath: '/minecraft/maps:ro'
            }
          ]
        }
      ],
      volumes: [
        {
          name: 'maps',
          hostPath: {
            path: '/storage/maps-private'
          }
        }
      ],
      restartPolicy: 'Never'
    }
    cluster.create_pod(pod)
  end

  def cluster
    @cluster ||= begin
      cluster_internal
    rescue
      cluster_external
    end
  end

  # Access the cluster from inside a pod that has a service account.
  def cluster_internal
    Kubeclient::Client.new(
      "https://kubernetes.default.svc",
      "v1",
      {
        ssl_options: {
          ca_file: "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"
        },
        auth_options: {
          bearer_token_file: "/var/run/secrets/kubernetes.io/serviceaccount/token"
        },
        socket_options: {
          socket_class: Celluloid::IO::TCPSocket,
          ssl_socket_class: Celluloid::IO::SSLSocket
        }
      }
    )
  end

  # Access the cluster from an external machine.
  def cluster_external
    config = Kubeclient::Config.read(File.expand_path("~/.kube/config"))
    context = config.context
    ssl_options = context.ssl_options
    ssl_options[:verify_ssl] = 0
    Kubeclient::Client.new(
      context.api_endpoint,
      context.api_version,
      {
        ssl_options: ssl_options,
        auth_options: context.auth_options,
        socket_options: {
          socket_class: Celluloid::IO::TCPSocket,
          ssl_socket_class: Celluloid::IO::SSLSocket
        }
      }
    )
  end
end
