// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.util;

import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.util.Elastic.Notification.NotificationLevel;
import java.util.ArrayList;
import java.util.List;

public abstract class ExpandedSubsystem extends SubsystemBase {
  protected List<Alert> prematchAlerts = new ArrayList<Alert>();
  protected String systemStatus = "Pre-Match not ran";

  public final void cancelCurrentCommand() {
    // Command currentCommand = getCurrentCommand();
    // Command defaultCommand = getDefaultCommand();

    // if (currentCommand != null && !(defaultCommand != null && currentCommand == defaultCommand))
    // {
    //   currentCommand.cancel();
    // }
  }

  public final String getAlertGroup() {
    return getName() + "/Alerts";
  }

  public void clearAlerts() {
    for (Alert alert : prematchAlerts) {
      alert.close();
    }

    prematchAlerts.clear();
  }

  private final void addAlert(Alert alert) {
    alert.set(true);
    prematchAlerts.add(alert);
  }

  public final void addInfo(String message) {
    addAlert(new Alert(getAlertGroup(), message, AlertType.kInfo));
  }

  public final void addWarning(String message) {
    addAlert(new Alert(getAlertGroup(), message, AlertType.kWarning));
    Elastic.sendNotification(
        new Elastic.Notification()
            .withLevel(NotificationLevel.WARNING)
            .withTitle(getName() + " Pre-Match Warning")
            .withDescription(message)
            .withDisplaySeconds(5));
  }

  public final void addError(String message) {
    addAlert(new Alert(getAlertGroup(), message, AlertType.kError));
    setSystemStatus("Pre-Match failed with reason: \"" + message + "\"");
    Elastic.sendNotification(
        new Elastic.Notification()
            .withLevel(NotificationLevel.ERROR)
            .withTitle(getName() + " Pre-Match Failed")
            .withDescription(message)
            .withDisplaySeconds(5));
  }

  public final void setSystemStatus(String status) {
    systemStatus = status;
  }

  public final String getSystemStatus() {
    return systemStatus;
  }

  public final boolean containsErrors() {
    for (Alert alert : prematchAlerts) {
      if (alert.getType() == AlertType.kError) {
        return true;
      }
    }

    return false;
  }

  public Command getPrematchCheckCommand() {
    return Commands.none();
  }

  public Command buildPrematch() {
    return Commands.sequence(
            Commands.runOnce(
                () -> {
                  cancelCurrentCommand();
                  clearAlerts();
                  setSystemStatus("Running Pre-Match Check");
                }),
            getPrematchCheckCommand())
        .until(this::containsErrors)
        .finallyDo(
            (interrupted) -> {
              cancelCurrentCommand();

              if (interrupted && !containsErrors()) {
                addError("Pre-Match Interrpted");
              } else if (!interrupted && !containsErrors()) {
                setSystemStatus("Pre-Match Successful!");
                Elastic.sendNotification(
                    new Elastic.Notification()
                        .withLevel(NotificationLevel.INFO)
                        .withTitle(getName() + " Pre-Match Successful")
                        .withDescription(
                            getName() + " Pre-Match was successful, good luck in the next match!")
                        .withDisplaySeconds(3.5));
              }
            });
  }
}
