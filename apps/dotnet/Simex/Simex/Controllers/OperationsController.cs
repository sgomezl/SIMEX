using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Simex.Models;
// using Simex.Dtos.Operations;

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

    [HttpGet("my-operations")]
    public async Task<ActionResult<IEnumerable<OperationDto>>> GetMyOperations()
    {
        var userIdClaim = User.FindFirstValue(ClaimTypes.NameIdentifier);

        if (!int.TryParse(userIdClaim, out var userId))
        {
            return Unauthorized(new { message = "Token invalido." });
        }

        var operations = await _context.Operations
            .Where(op => op.DOCUMENT_USER_ID == userId)
            .ToListAsync();

        if (operations == null || !operations.Any())
        {
            return Ok(new List<OperationDto>());
        }

        var operationsDto = operations.Select(op => new OperationDto
        {
            Id = op.Id,
            DocumentUserId = op.DOCUMENT_USER_ID,
            // Description = op.Description,
            // Status = op.Status,
            // Date = op.Date
        }).ToList();

        return Ok(operationsDto);
    }
}