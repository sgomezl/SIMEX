using System;
using System.Collections.Generic;

namespace Simex.Models;

public partial class Dua
{
    public int Id { get; set; }

    public int OperationId { get; set; }

    public int DuaStateId { get; set; }

    public int CreateUserId { get; set; }

    public string? DocumentNumber { get; set; }

    public DateTime CreationDate { get; set; }

    public DateTime? PresentationDate { get; set; }

    public string Origin { get; set; } = null!;

    public string Destination { get; set; } = null!;

    public string? SpecificDestination { get; set; }

    public string? GoodsLocation { get; set; }

    public decimal GrossWeight { get; set; }

    public virtual User CreateUser { get; set; } = null!;

    public virtual DuaState DuaState { get; set; } = null!;

    public virtual ICollection<DuaStateHistory> DuaStateHistories { get; set; } = new List<DuaStateHistory>();

    public virtual Operation Operation { get; set; } = null!;
}
