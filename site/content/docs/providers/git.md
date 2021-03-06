+++
title = "Git provider"
description = "How to use the git provider"
date = 2021-01-13T17:37:44Z
weight = 20
draft = false
bref = ""
toc = true
+++

# Git provider

## How does it work

The git provider is used to retrieve specification files from **git** repositories.

Each repositories are *cloned* then *pulled* at regular interval (`cache-duration`).

The repositories can be set manually or be discovered from **Github**.

## Prerequisite

This provider requires `git` to be available on the host.

Also if you are intending to use Protobuf specification files, you need to have `protoc` available.

If you are using the docker image, there is **no need** to install them.

## Default configuration

```ocon
unisonui {
  providers += "tech.unisonui.providers.GitProvider"

  provider.git {
    cache-duration = "2 hours" // Interval between each clone....
    vcs {
      // Discover repositories through Github API
      github {
        api-token = "" // Github personal token.
        api-uri = "https://api.github.com/graphql" // Github GraphQL url.
        polling-interval = "1 hour" // Interval between each polling.
        repositories = [] // List of repositories.
      }
      git {
        repositories = [] // List of repositories
      }
    }
  }
}
```

Either you choose using **Github** discovery or plain **git** repositories,
each option requires a list of `repositories`.
This list can be either a **string** corresponding to
the full URL/Regex (`organization/project` for Github) or on an object.

The object follows the following schema:

```hocon
{
  location = "" // Full url, `organization/project` for Github
                // or a regex (the string **MUST** starts and ends with `/`)
  branch = "" // Branch to clone (default to `master` or inferred from the default branch in Github)
}
```

## Specifications detection

If the repository contains a file at the root level called `.unisonui.yaml` then the Git provider will
detect the specification files accordingly.

The git provider currently support two versions of this file.

### Version 2

```yaml
version: "2"

# Optional service's name.
# If not provided the name will be inferred from the repository URL
# Example: "https://github.com/MyOrg/MyRepo" -> "MyOrg/MyOrg"
name: "My service"

openapi:
  # Enable or disable the proxy for all specification files.
  # It's disabled by default.
  # Enable it if your endpoint doesn't support CORS
  useProxy: false

  # List of specifications
  # Can be either a string representing the path where files are located
  # or a an object with advanced configuration.
  specifications:
    - "foo-service.yaml"
    - "/directory_contening_several_files/"
    - name: "Name used for this file" # Override the service's name for this file
      path: "foobar.yaml" # File path
      useProxy: true # Override the proxy configuration

grpc:
  # List of endpoints where your service lives.
  servers:
    - address: 127.0.0.1 # IP or hostname
      port: 8080 # Port
      # Enable TLS communication.
      # False by default.
      # Warning, be sure to make your self-certificate available to UnisonUI.
      useTls: false
      # Optional name for this endpoint.
      # If omitted, the endpoint's name will be: host:port
      name: "Dev"
  # List of protobufs files
  # It requires a map. "protobuf file path" -> object
  protobufs:
    "path/spec.proto": {}
    "path/spec2.proto":
      name: test # Override the service's name for this file
      # Override ENTIRELY the servers list for this file
      servers:
        - address: 127.0.0.1
          port: 8080
          name: other server
          useTls: true
```

### Version 1

Only file named `.restui.yaml` instead of `.unisonui.yaml` supports this version, for retro compatibility reason.

```yaml
# Service's name.
# If this field does not provide the service name will be inferred from the repository URL
# Example: "https://github.com/MyOrg/MyRepo" -> "MyOrg/MyOrg"
name: "service name"

# useProxy activate the proxy for the interface. Otherwise your service might needs to activate CORS
useProxy: true

# List of OpenApi spec files or directories
# This list can be a mixed of string (path)
# or an object:
#   name: Name of this service
#   useProxy: activate the proxy for the interface. Otherwise your service might needs to activate CORS
specifications: []
```

## Github configuration

If you intend to use the Github repositories discovery you need to provide a token.

This token can be generated *[here](https://github.com/settings/tokens/new)*.

You will need to allow:

* `public_repo` if you want to list only public repositories
* `repo` if want to list public and private repositories
