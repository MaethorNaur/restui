defmodule GitProvider.Git.Events.Delete do
    @type t :: %__MODULE__{path: String.t()}
    defstruct [:path]

    defimpl GitProvider.Git.Event, for: __MODULE__ do
      alias GitProvider.Git.Events.Delete
      alias GitProvider.Git.Repository
      alias Common.Events.Down

      def to_event(%Delete{path: path}, %Repository{name: name, directory: directory}) do
        path = String.replace_prefix(path, directory, "") |> String.trim_leading("/")
        %Down{id: "#{name}:#{path}"}
      end

      def load_content(_), do: :ignore
    end
  end
