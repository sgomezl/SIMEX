using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Simex.Dtos;
using Simex.Models;

namespace Simex.Controllers;

[ApiController]
[Route("api/operations")]
[Authorize]
public class OperationsController : ControllerBase
{
    private readonly Simex04Context _context;

    public OperationsController(Simex04Context context)
    {
        _context = context;
    }

    [HttpGet("user-operations")]
    public async Task<ActionResult<IEnumerable<OperationDto>>> GetUserOperations()
    {
        // extracción del ID del usuario del token
        var userIdClaim = User.FindFirstValue(ClaimTypes.NameIdentifier);

        if (!int.TryParse(userIdClaim, out var userId))
        {
            return Unauthorized(new { message = "Token invalido." });
        }

        // AsNoTracking(): solo vamos a leer, esto hace la consulta más rápida
        var user = await _context.Users
            .AsNoTracking()
            .FirstOrDefaultAsync(u => u.Id == userId);

        if (user == null || user.CompanyId == null)
        {
            return Ok(new List<OperationDto>());
        }

        var operations = await _context.Operations
            .Where(op => op.NavieraId == user.CompanyId)
            .ToListAsync();

        if (!operations.Any())
        {
            return Ok(new List<OperationDto>());
        }

        var operationsDto = operations.Select(op => new OperationDto
        {
            Id = op.Id,
            OriginPortId = op.OriginPortId,
            DestinationPortId = op.DestinationPortId,
            TotalCost = op.TotalCost,
            Etd = op.Etd,
            Eta = op.Eta,
            IncotermId = op.IncotermId,
            PiecesNumber = op.PiecesNumber,
            Kilograms = op.Kilograms
        }).ToList();

        return Ok(operationsDto);
    }
}