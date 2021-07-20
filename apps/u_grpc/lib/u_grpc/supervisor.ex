defmodule UGRPC.ClientSupervisor do
  use GenServer

  def start_link(_args), do: GenServer.start_link(__MODULE__, nil, name: __MODULE__)

  @impl true
  def init(_args), do: {:ok, %{}}

  @spec new_client(server :: String.t()) :: {:ok, UGRPC.Client.Connection.t()} | {:error, term()}

  def new_client(server) do
    try do
      GenServer.call(__MODULE__, {:new_client, server})
    catch
      :exit, {e, _} -> {:error, e}
    end
  end

  @impl true
  def handle_call({:new_client, server}, _from, state) do
    case Map.get(state, server) do
      nil ->
        case start_child(server) do
          {:ok, pid} ->
            stream = %UGRPC.Client.Connection{pid: pid}
            {:reply, {:ok, stream}, Map.put(state, server, stream)}

          error ->
            {:reply, error, state}
        end

      stream ->
        {:reply, {:ok, stream}, state}
    end
  end

  @impl true
  def handle_info({:DOWN, _ref, :process, pid, _reason}, state),
    do:
      {:noreply,
       state
       |> Stream.reject(fn {_, %UGRPC.Client.Connection{pid: p}} -> p == pid end)
       |> Enum.into(%{})}

  defp start_child(server) do
    case UGRPC.Client.start_link(server) do
      {:ok, pid} ->
        _ = Process.unlink(pid)
        _ = Process.monitor(pid)
        {:ok, pid}

      error ->
        error
    end
  end
end
