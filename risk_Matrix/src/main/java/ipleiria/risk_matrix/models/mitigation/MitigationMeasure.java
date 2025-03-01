package ipleiria.risk_matrix.models.mitigation;
import ipleiria.risk_matrix.models.riskAssessment.RiskAssessment;
import jakarta.persistence.*;

@Entity
@Table(name = "mitigation_measures")
public class MitigationMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "risk_assessment_id", nullable = false)
    private RiskAssessment riskAssessment;

    // Constructors
    public MitigationMeasure() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }
}
