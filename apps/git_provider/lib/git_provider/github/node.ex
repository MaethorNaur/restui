defmodule GitProvider.Github.Node do
  @type t :: %__MODULE__{name: String.t(), url: String.t(), branch: String.t()}

  defstruct [:name, :url, :branch]
end