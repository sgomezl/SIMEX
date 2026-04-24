using System.ComponentModel.DataAnnotations;

namespace Simex.Dtos;

public class RejectOperationRequestDto
{
    [Required]
    [StringLength(255)]
    public string Reason { get; set; } = string.Empty;
}
