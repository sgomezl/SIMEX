using System.ComponentModel.DataAnnotations;

namespace Simex.Dtos;

public class AdvanceOperationTrackingStepRequestDto
{
    public DateTime? ArrivedAt { get; set; }

    [StringLength(255)]
    public string? Observations { get; set; }
}
