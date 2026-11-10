using Bitacora.Api.Middleware;
using Bitacora.Application.Services;
using Bitacora.Infrastructure;
using Microsoft.Data.Sqlite;

var builder = WebApplication.CreateBuilder(args);


var csb = new SqliteConnectionStringBuilder(
    builder.Configuration.GetConnectionString("Bitacora") ?? "Data Source=bitacora.db");
if (!Path.IsPathRooted(csb.DataSource))
    csb.DataSource = Path.Combine(builder.Environment.ContentRootPath, csb.DataSource);

builder.Services.AddInfraestructura(csb.ToString());
builder.Services.AddScoped<IRegistroService, RegistroService>();

builder.Services.AddControllers(o =>
{
    o.ReturnHttpNotAcceptable = true;
});
builder.Services.AddProblemDetails();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new() { Title = "Bitácora API", Version = "v1" });
});

var app = builder.Build();

app.UseMiddleware<ExceptionHandlingMiddleware>();

app.UseSwagger();
app.UseSwaggerUI(c =>
{
    c.SwaggerEndpoint("/swagger/v1/swagger.json", "Bitácora API v1");
});

app.UseStatusCodePages();
app.MapControllers();
app.MapGet("/", () => Results.Redirect("/swagger")).ExcludeFromDescription();

app.Run();
