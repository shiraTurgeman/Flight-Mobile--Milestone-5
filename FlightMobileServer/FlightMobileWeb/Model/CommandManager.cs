using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Concurrent;
using System.Text.Json;
using System.Threading.Tasks;

namespace FlightMobileWeb.Model
{
    public class CommandManager:ICommandManager
    {

        private readonly BlockingCollection<AsyncCommand> queue;
        //variable of the class.
        ITelnetClient telnetClient;
        volatile Boolean stop;
        IConfiguration configuration;

        //constroctor.
        public CommandManager(ITelnetClient telnetClient, IConfiguration configuration)
        {
            this.telnetClient = telnetClient;
            this.configuration = configuration;
            this.queue = new BlockingCollection<AsyncCommand>();
            stop = true;
            Start();
        }

        //this function connects the model to the server.
        public void connect(string ip, int port)
        {
            var task = Task.Run(() => telnetClient.connect(ip, port));
            if (task.Wait(TimeSpan.FromSeconds(10)))
            {
                stop = false;
            }
            else
            {
                throw new Exception("Theres no information from the\nserver for over 10 seconds.");
            }

        }
        //this function disconnect the model to the server.
        public void disconnect()
        {
            stop = true;
            telnetClient.disconnect();
        }
        public bool isConnect()
        {
            return stop;
        }


        //return the boolean value of stop.
        public bool getStop()
        {
            return this.stop;
        }


        public void write(string command)
        {
            telnetClient.write(command);
        }

        public string read()
        {
            return telnetClient.read();
        }

        public void setValue(ITelnetClient myTelnetClient, string property, double value)
        {
            ITelnetClient telnetClient = myTelnetClient;
            string set = null;
            if (property.Equals("throttle"))
            {
                set = "set /controls/engines/current-engine/" + property + " " + value + "\r\n";
            }
            else
            {
                set = "set /controls/flight/" + property + " " + value + "\r\n";
            }
            telnetClient.write(set);
        }
        public string checkSet(ITelnetClient myTelnetClient, string property, double value)
        {
            ITelnetClient telnetClient = myTelnetClient;
            string get = null;
            if (property.Equals("throttle"))
            {
                get = "get /controls/engines/current-engine/" + property + "\r\n";
            }
            else
            {
                get = "get /controls/flight/" + property + "\r\n";
            }
            telnetClient.write(get);
            
            string retValue = telnetClient.read();
            Console.WriteLine(retValue);

            string s;
            double newRet = Convert.ToDouble(retValue);
            if (retValue.ToString().Length > 7)
            {
                s = retValue.ToString().Substring(0, 7);
                newRet = Convert.ToDouble(s);
            }

            double newVal = value;
            if (value.ToString().Length > 7)
            {
                s = value.ToString().Substring(0, 7);
                newVal = Convert.ToDouble(s);
            }


            if (newRet == newVal)
            {
                return "Ok";
            }
            else
            {
                return "NotOk";
            }
        }
        public Command ParseJesonCommand(JsonElement jsonCommand)
        {

            Command commandObj = new Command();

            string strCommand = System.Text.Json.JsonSerializer.Serialize(jsonCommand);
            JObject data = JObject.Parse(strCommand);
            JToken jToken = data;

            commandObj.aileron = (double)jToken["aileron"];
            commandObj.rudder = (double)jToken["rudder"];
            commandObj.elevator = (double)jToken["elevator"];
            commandObj.throttle = (double)jToken["throttle"];

            return commandObj;
        }

        public Task<Result> Execute(Command cmd)
        {
            var asyncCommand = new AsyncCommand(cmd);
            queue.Add(asyncCommand);
            return asyncCommand.Task;
        }

        public void Start()
        {
            Task.Factory.StartNew(ProcessCommands);
        }


        public void ProcessCommands()
        {
            string ip = configuration.GetValue<string>("flightGearIP");
            int port = Convert.ToInt32(configuration.GetValue<string>("flightGearPort"));
            telnetClient.connect(ip, port);
            //            telnetClient.connect("127.0.0.1", 5402);
            telnetClient.write("data\n");
            foreach (AsyncCommand command in queue.GetConsumingEnumerable())
            {
                //rudder
                setValue(telnetClient, "rudder", command.Command.rudder);
                string checkRudder = checkSet(telnetClient, "rudder", command.Command.rudder);
                //throttle
                setValue(telnetClient, "throttle", command.Command.throttle);
                string checkThrottle = checkSet(telnetClient, "throttle", command.Command.throttle);
                //aileron
                setValue(telnetClient, "aileron", command.Command.aileron);
                string checkAileron = checkSet(telnetClient, "aileron", command.Command.aileron);
                //elevator
                setValue(telnetClient, "elevator", command.Command.elevator);
                string checkElevator = checkSet(telnetClient, "elevator", command.Command.elevator);
                Result res;
                if ((String.Compare(checkAileron, "Ok") == 0) && (String.Compare(checkElevator, "Ok") == 0) &&
                    (String.Compare(checkRudder, "Ok") == 0) && (String.Compare(checkThrottle, "Ok") == 0))
                {
                    res = Result.Ok;
                }
                else
                {
                    res = Result.NotOk;
                }
                command.Completion.SetResult(res);
            }
        }
    }
}
