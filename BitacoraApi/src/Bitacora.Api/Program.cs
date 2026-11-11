using Bitacora.Api.Middleware;
using Bitacora.Api.Seguridad;
using Bitacora.Application.Services;
using Bitacora.Infrastructure;
using Bitacora.Infrastructure.Seguridad;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.Data.Sqlite;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);

var csb = new SqliteConnectionStringBuilder(
    builder.Configuration.GetConnectionString("Bitacora") ?? "Data Source=bitacora.db");
if (!Path.IsPathRooted(csb.DataSource))
    csb.DataSource = Path.Combine(builder.Environment.ContentRootPath, csb.DataSource);

var seccionJwt = builder.Configuration.GetSection(JwtOpciones.Seccion);
var jwt = seccionJwt.Get<JwtOpciones>() ?? new JwtOpciones();
jwt.Validar();
builder.Services.Configure<JwtOpciones>(seccionJwt);

builder.Services.AddInfraestructura(csb.ToString());
builder.Services.AddScoped<IRegistroService, RegistroService>();
builder.Services.AddScoped<IAuthService, AuthService>();

builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(o =>
    {
        o.MapInboundClaims = false;
        o.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = jwt.Issuer,
            ValidateAudience = true,
            ValidAudience = jwt.Audience,
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = jwt.CrearClave(),
            ValidAlgorithms = new[] { SecurityAlgorithms.HmacSha256 },
            ValidateLifetime = true,
            RequireExpirationTime = true,
            ClockSkew = TimeSpan.Zero,
            NameClaimType = JwtOpciones.ClaimCorreo,
            RoleClaimType = JwtOpciones.ClaimRol
        };
        o.Events = new JwtBearerEvents
        {
            OnChallenge = async ctx =>
            {
                ctx.HandleResponse();
                await RespuestasSeguridad.EscribirNoAutenticadoAsync(ctx.HttpContext, ctx.AuthenticateFailure);
            },
            OnForbidden = ctx => RespuestasSeguridad.EscribirProhibidoAsync(ctx.HttpContext)
        };
    });
builder.Services.AddAuthorization();

builder.Services.AddHttpsRedirection(o =>
{
    o.HttpsPort = builder.Configuration.GetValue<int?>("HttpsPort") ?? 7080;
});
builder.Services.AddHsts(o => o.MaxAge = TimeSpan.FromDays(365));

builder.Services.AddControllers(o =>
{
    o.ReturnHttpNotAcceptable = true;
});
builder.Services.AddProblemDetails();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new() { Title = "Bitácora API", Version = "v1" });
    c.AddSecurityDefinition(SeguridadSwaggerFilter.Esquema, new OpenApiSecurityScheme
    {
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        In = ParameterLocation.Header,
        Name = "Authorization",
        Description = "Pegue solo el token devuelto por POST /api/auth/login (sin la palabra Bearer)."
    });
    c.OperationFilter<SeguridadSwaggerFilter>();
});

var app = builder.Build();

await app.Services.AplicarMigracionesAsync();

var adminCorreo = app.Configuration["Admin:Correo"];
var adminPassword = app.Configuration["Admin:Password"];
if (!string.IsNullOrWhiteSpace(adminCorreo) && !string.IsNullOrWhiteSpace(adminPassword))
{
    using var scope = app.Services.CreateScope();
    await scope.ServiceProvider.GetRequiredService<IAuthService>().AsegurarAdminAsync(adminCorreo, adminPassword);
}
else
{
    app.Logger.LogWarning(
        "No se creó el usuario Admin semilla: configure Admin:Correo y Admin:Password con dotnet user-secrets.");
}

app.UseMiddleware<ExceptionHandlingMiddleware>();

if (!app.Environment.IsDevelopment())
    app.UseHsts();

var hostsSinRedireccion = app.Configuration.GetSection("HostsSinRedireccionHttps").Get<string[]>() ?? [];
app.UseWhen(
    ctx => !hostsSinRedireccion.Contains(ctx.Request.Host.Host, StringComparer.OrdinalIgnoreCase),
    rama => rama.UseHttpsRedirection());

app.UseSwagger();
app.UseSwaggerUI(c =>
{
    c.SwaggerEndpoint("/swagger/v1/swagger.json", "Bitácora API v1");
});

app.UseStatusCodePages();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();
app.MapGet("/", () => Results.Redirect("/swagger")).ExcludeFromDescription();

app.Run();
