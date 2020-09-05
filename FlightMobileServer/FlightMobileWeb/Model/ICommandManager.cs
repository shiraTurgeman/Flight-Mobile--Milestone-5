using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;

namespace FlightMobileWeb.Model
{
    public interface ICommandManager
    {
        //funcions for the definiton of model.
        void connect(string ip, int port);
        void disconnect();
        bool isConnect();

        bool getStop();

        void write(string command);
        string read();
        Command ParseJesonCommand(JsonElement jsonCommand);
        Task<Result> Execute(Command cmd);
    }
}
