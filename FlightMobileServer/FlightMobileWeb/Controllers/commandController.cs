using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;
using FlightMobileWeb.Model;
using Microsoft.AspNetCore.Mvc;


namespace FlightMobileWeb.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class commandController : ControllerBase
    {

        private ICommandManager commandManager;
        //constructor
        public commandController(ICommandManager cm)
        {
            this.commandManager = cm;
        }

        // POST api/<commandController>
        [HttpPost]
        public IActionResult Post(JsonElement jsonCommand)
        {
            try
            {
                Command cmd = commandManager.ParseJesonCommand(jsonCommand);
                Task<Result> res = commandManager.Execute(cmd);

                if (String.Compare(res.Result.ToString(), "Ok") == 0)
                {
                    return Ok();
                }
                else
                {
                    throw new Exception("NotOk");
                }

            }
            catch (Exception e)
            {
                return NotFound(e.Message);
            }

        }
    }
}
