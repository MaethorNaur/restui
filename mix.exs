defmodule Unisonui.MixProject do
  @version "2.0.0"
  use Mix.Project

  defp common_apps,
    do: [
      clustering: :permanent,
      services: :permanent,
      configuration: :permanent,
      common: :permanent,
      u_grpc: :permanent
    ]

  defp providers, do: [git_provider: :permanent, container_provider: :permanent]

  defp providers_apps(:all), do: common_apps() ++ providers()
  defp providers_apps(provider), do: [{provider, :permanent} | common_apps()]

  defp web_apps, do: [unison_ui: :permanent]

  defp all_apps, do: common_apps() ++ providers() ++ web_apps()

  defp provider_release(name),
    do: [
      steps: [:assemble],
      applications: providers_apps(name),
      config_providers: [
        {Toml.Provider,
         [
           path: {:system, "UNISON_UI_ROOT", "/config.toml"},
           transforms: [Configuration.LoggingTransformer]
         ]}
      ]
    ]

  def project do
    [
      apps_path: "apps",
      version: "2.0.0",
      start_permanent: Mix.env() == :prod,
      deps: deps(),
      test_coverage: [tool: ExCoveralls],
      preferred_cli_env: ["coveralls.html": :test, "coveralls.json": :test],
      releases: [
        unisonui_git_provider: provider_release(:git_provider),
        unisonui_container_provider: provider_release(:container_provider),
        unisonui: [
          steps: [:assemble],
          applications: all_apps(),
          config_providers: [
            {Toml.Provider,
             [
               path: {:system, "UNISON_UI_ROOT", "/config.toml"},
               transforms: [Configuration.LoggingTransformer]
             ]}
          ]
        ]
      ],
      dialyzer: [
        plt_file: {:no_warn, "priv/plts/dialyzer.plt"},
        flags: [
          "-Wunmatched_returns",
          "-Werror_handling",
          "-Wrace_conditions",
          "-Wno_opaque",
          "-Wunderspecs"
        ]
      ]
    ]
  end

  def npm_deploy(release) do
    System.cmd("npm", ["install"], cd: "apps/unison_ui/webapp/")
    System.cmd("npm", ["run", "build:#{Mix.env()}"], cd: "apps/unison_ui/webapp/")
    release
  end

  # Dependencies listed here are available only for this
  # project and cannot be accessed from applications inside
  # the apps folder.
  #
  # Run "mix help deps" for examples and options.
  defp deps do
    [
      {:toml, "~> 0.6"},
      {:version_tasks, "~> 0.12.0", only: [:dev], runtime: false},
      {:credo, "~> 1.4", only: [:dev], runtime: false},
      {:dialyxir, "~> 1.0.0", only: [:dev], runtime: false},
      {:ex_doc, "~> 0.21", only: [:dev], runtime: false},
      {:excoveralls, "~> 0.14", only: [:dev, :test], runtime: false},
      {:inch_ex, "~> 2.1.0-rc.1", only: [:dev, :test], runtime: false}
    ]
  end
end